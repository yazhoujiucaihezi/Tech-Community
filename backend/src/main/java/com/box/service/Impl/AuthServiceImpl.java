package com.box.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.box.constant.RedisConstant;
import com.box.dto.LoginDTO;
import com.box.dto.RegisterDTO;
import com.box.entity.EmailRecord;
import com.box.entity.User;
import com.box.exception.BusinessException;
import com.box.mapper.AuthMapper;
import com.box.mapper.EmailMapper;
import com.box.service.AuthService;
import com.box.utils.JwtUtils;
import com.box.vo.LoginVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl extends ServiceImpl<AuthMapper, User> implements AuthService {

    private final AuthMapper authMapper;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final StringRedisTemplate stringRedisTemplate;

    private final JavaMailSender javaMailSender;

    private final EmailMapper emailMapper;

    private final JwtUtils jwtUtils;

    @Value("${jwt.secret}")
    private String secret;
    /**
     * 用户登录
     */
    @Override
    public LoginVO login(LoginDTO dto){
        LoginVO vo = new LoginVO();
        User user = authMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())
        );
        if(user == null || !bCryptPasswordEncoder.matches(dto.getPassword(), user.getPassword())){
            throw new BusinessException("账号不存在或密码错误");
        }
        if (user.getBanned() == 1){
            throw new BusinessException("账号已封禁");
        }

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Date date = new Date(System.currentTimeMillis() + 7 * 24 * 3600 * 1000L);
        String token = Jwts.builder()
                .claim("uid", user.getId())
                .claim("username", user.getUsername())
                .claim("role", user.getRole())
                .expiration(date)
                .signWith(key)
                .id(user.getId()+UUID.randomUUID().toString())
                .compact();
        vo.setToken(token);
        vo.setExpire(date);
        vo.setRole(user.getRole());
        vo.setUsername(user.getUsername());
        return vo;
    }

    /**
     * 获取验证码
     */
    @Override
    public void askCode(String email, String type){
        if (stringRedisTemplate.opsForValue().get(email+"Repeat") != null){
            throw new BusinessException("请稍后再试");
        }
        EmailRecord emailRecord = new EmailRecord();
        SecureRandom secureRandom = new SecureRandom();
        String failKey = email+":"+type+"fail_count";
        if ("register".equals(type) && authMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail,email)) != null) {
            stringRedisTemplate.delete(email+":"+ "register");
            stringRedisTemplate.delete(failKey);
            stringRedisTemplate.opsForValue().set(email+"Repeat","mj",1,TimeUnit.MINUTES);
            throw new BusinessException("邮箱已存在");
        }
        if (stringRedisTemplate.opsForValue().get(failKey)!=null){
            stringRedisTemplate.delete(email+":"+type+"fail_count");
        }
        if ("reset".equals(type)) {
            User user = authMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getEmail, email)
            );
            if (user == null) {
                throw new BusinessException("该邮箱未注册");
            }
        }
        int code = 100000 + secureRandom.nextInt(900000);
        SimpleMailMessage message = getSimpleMailMessage(email, type, code);
        BeanUtils.copyProperties(message,emailRecord);
        emailRecord.setContent(message.getText());
        emailRecord.setTime(LocalDateTime.now());
        emailRecord.setStatus(1);
        emailRecord.setEmail(email);
        emailRecord.setTitle(message.getSubject());
        emailMapper.insert(emailRecord);
        javaMailSender.send(message);
        stringRedisTemplate.opsForValue().set(email+":"+type, String.valueOf(code),3, TimeUnit.MINUTES);
    }


    /**
     * 用户注册
     */
    @Override
    public void register(RegisterDTO dto) {
        verifyCode(dto.getCode(),dto.getEmail(),"register");
        if (authMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername,dto.getUsername())) != null) {
            throw new BusinessException("用户名已存在");
        }
        String encoded =bCryptPasswordEncoder.encode(dto.getPassword());

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(encoded);
        user.setEmail(dto.getEmail());
        user.setRole("user");
        user.setMute(0);
        user.setBanned(0);
        user.setCreateTime(LocalDateTime.now());
        authMapper.insert(user);
        stringRedisTemplate.delete(dto.getEmail() + ":register");
        emailStatus(dto);

    }


    /**
     * 重置密码
     */
    @Override
    public void resetPassword(RegisterDTO dto) {
        User user = resetConfirm(dto);
        String encoded =bCryptPasswordEncoder.encode(dto.getPassword());
        user.setPassword(encoded);
        authMapper.updateById(user);
        stringRedisTemplate.delete(dto.getEmail()+":"+"reset");
        emailStatus(dto);
    }

    /**
     * 退出登录
     */
    @Override
    public void logout(String authHeader) {
        Claims token = jwtUtils.parse(authHeader);
        long ttl = token.getExpiration().getTime() - System.currentTimeMillis();
        if (ttl > 0){
            stringRedisTemplate.opsForValue().set(RedisConstant.blackList + token.getId(),"1",ttl,TimeUnit.MILLISECONDS);
        }

    }

    private void emailStatus(RegisterDTO dto) {
        LambdaUpdateWrapper<EmailRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(EmailRecord::getEmail, dto.getEmail());
        wrapper.set(EmailRecord::getStatus, 2);
        emailMapper.update(null, wrapper);
    }

    @NotNull
    public static SimpleMailMessage getSimpleMailMessage(String email, String type, int code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("2464859727@qq.com");     // 发件人
        message.setTo(email);                     // 收件人
        if ("register".equals(type)) {
            message.setSubject("注册验证邮件");
            message.setText("您正在注册极客技术社区账号，验证码：  " + code + "，3分钟内有效。");
        } else if ("modify".equals(type)) {
            message.setSubject("邮箱修改验证邮件");
            message.setText("您正在绑定新的电子邮箱，验证码：  " + code + "，3分钟内有效。");
        }else {
            message.setSubject("验证邮件");
            message.setText("您的验证码：" + code + "，3分钟内有效。");
        }
        return message;
    }

    /**
     * 重置密码校验
     */
    @Override
    public User resetConfirm(RegisterDTO dto) {
        User user = authMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail()));
        if (user == null) {
            stringRedisTemplate.opsForValue().set(dto.getEmail()+"Repeat","mj",1,TimeUnit.MINUTES);
            throw new BusinessException("邮箱不存在");
        }
        verifyCode(dto.getCode(),dto.getEmail(),"reset");
        return user;
    }

    //------方法------

    private void verifyCode(String code,String email,String type){
        String rightCode = stringRedisTemplate.opsForValue().get(email +":"+ type);
        String failKey = email+":"+type+"fail_count";
        if (rightCode == null) {
            throw new BusinessException("验证码已失效");
        }
        if (code == null){
            throw new BusinessException("请输入验证码");
        }
        if (!code.equals(rightCode)) {
            Long increment = stringRedisTemplate.opsForValue().increment(failKey);

            if (increment != null && increment == 1) {
                stringRedisTemplate.expire(failKey, 15, TimeUnit.MINUTES);
            }

            if (increment!=null && increment == 5){
                stringRedisTemplate.delete(email +":"+ type);
                stringRedisTemplate.delete(failKey);
                throw new BusinessException("验证码错误次数过多,请重发验证码");
            }

            long remainCount = 5 - (increment == null ? 1 : increment);
            throw new BusinessException("验证码错误,您还有" + remainCount +"次机会");
        }
    }

}
