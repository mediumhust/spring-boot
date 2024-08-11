package com.example.demo.service;

import com.example.demo.constant.PredefinedRole;
import com.example.demo.dto.request.UserCreationRequest;
import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private UserMapper userMapper;

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;


    public User createUser(UserCreationRequest request){
//        User user = new User();

        if(userRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTED);
//            throw new RuntimeException("ErrorCode.USER_EXISTED");

//        UserCreationRequest request1 = UserCreationRequest.builder()
//                        .username("")
//                                .firstname("").build();

//        user.setUsername(request.getUsername());
//        user.setPassword(request.getPassword());
//        user.setFirstname(request.getFirstname());
//        user.setLastname(request.getLastname());
//        user.setDob(request.getDob());

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    public List<UserResponse> getUers(){
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse).toList();
    }

    @PostAuthorize("returnObject.username == authentication.name || hasRole('ADMIN')")
    public UserResponse getUser(String id){
        return  userMapper.toUserResponse(userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    public UserResponse getMyInfo(){
        var username = SecurityContextHolder.getContext().getAuthentication().getName();

        return  userMapper.toUserResponse(userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

//        user.setPassword(request.getPassword());
//        user.setFirstname(request.getFirstname());
//        user.setLastname(request.getLastname());
//        user.setDob(request.getDob());

        userMapper.updateUser(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        var roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public void deleteUser(String userId){
        userRepository.deleteById(userId);
    }
}
