package com.naveen.service;

import com.naveen.entity.UserEntity;
import com.naveen.io.ProfileRequest;
import com.naveen.io.ProfileResponse;
import com.naveen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        UserEntity newProfile = convertToUserEntity(request);
        if(userRepository.existsByEmail(request.getEmail())){
           throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already exists");
        }
        newProfile = userRepository.save(newProfile);
        return converToProfileResponse(newProfile);
    }

    @Override
    public ProfileResponse getProfile(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("User not found"+email));

        return converToProfileResponse(existingUser);
    }

    @Override
    public void sendResetOtp(String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("user not found : "+email));
//        Generate 6 digit otp
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000,1000000));
//        calculate expiry time (current time + 10 mins in ms)
        long expiryTime = System.currentTimeMillis() + (10 * 60 * 1000);
//        update the profile/user
        userEntity.setResetOtp(otp);
        userEntity.setResetOtpExpireAt(expiryTime);
//        save into db
        userRepository.save(userEntity);

        try{
//            send reset otp email
            emailService.sendResetOtpEmail(userEntity.getEmail(), userEntity.getName(), otp);
        }catch (Exception e){
            throw new RuntimeException("Unable to send email");
        }


    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("User not found : "+email));
        if (existingUser.getResetOtp() == null || !existingUser.getResetOtp().equals(otp)){
                throw new RuntimeException("Invalid OTP");
        }
        if (existingUser.getResetOtpExpireAt() < System.currentTimeMillis()) {
            throw new RuntimeException("OTP Expired");
        }
        existingUser.setPassword(passwordEncoder.encode(newPassword));
        existingUser.setResetOtp(null);
        existingUser.setResetOtpExpireAt(0L);

        userRepository.save(existingUser);

//      send password changed notification
        emailService.sendPasswordChangedNotification(existingUser.getEmail(), existingUser.getName());

    }

    @Override
    public void sendOtp(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(()->new RuntimeException("User not found : "+email));
        if(existingUser.getIsAccountVerified()!=null && existingUser.getIsAccountVerified()){
            return;
        }
//        Generate 6 digit OTP
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000,1000000));
//        calculate expiry time (current time + 24 hrs in ms)
        long expiryTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000);
//        update the user entity
        existingUser.setVerifyOtp(otp);
        existingUser.setVerifyOtpExpireAt(expiryTime);
//        save it to db
        userRepository.save(existingUser);

        try{
            emailService.sendOtpEmail(existingUser.getEmail(), existingUser.getName(), otp);
        } catch (Exception e){
            throw new RuntimeException("Unable to send email");
        }
    }

    @Override
    public void verifyOtp(String email, String otp) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("User not found : "+email));
        if (existingUser.getVerifyOtp() == null || !existingUser.getVerifyOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }
        if(existingUser.getVerifyOtpExpireAt() < System.currentTimeMillis()){
            throw new RuntimeException("OTP Expired");
        }
        existingUser.setIsAccountVerified(true);
        existingUser.setVerifyOtp(null);
        existingUser.setVerifyOtpExpireAt(0L);

        System.out.println("User email: " + email);
        System.out.println("Stored OTP: " + existingUser.getVerifyOtp());
        System.out.println("Received OTP: " + otp);
        System.out.println("OTP Expiry: " + existingUser.getVerifyOtpExpireAt());
        System.out.println("Current Time: " + System.currentTimeMillis());


        userRepository.save(existingUser);
    }

    private ProfileResponse converToProfileResponse(UserEntity newProfile) {
        return ProfileResponse.builder()
                .userId(newProfile.getUserId())
                .name(newProfile.getName())
                .email(newProfile.getEmail())
                .isAccountVerified(newProfile.getIsAccountVerified())
                .build();
    }

    private UserEntity convertToUserEntity(ProfileRequest request) {
        return UserEntity.builder()
                .name(request.getName())
                .email(request.getEmail())
                .userId(UUID.randomUUID().toString())
                .password(passwordEncoder.encode(request.getPassword()))
                .isAccountVerified(false)
                .resetOtpExpireAt(0L)
                .verifyOtp(null)
                .verifyOtpExpireAt(0L)
                .resetOtp(null)
                .build();
    }
}
