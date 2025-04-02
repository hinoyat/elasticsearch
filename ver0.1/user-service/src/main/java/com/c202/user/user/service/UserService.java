package com.c202.user.user.service;

import com.c202.user.user.model.request.UpdateIntroductionDto;
import com.c202.user.user.model.request.UpdateUserRequestDto;
import com.c202.user.user.model.response.UserProfileDto;
import com.c202.user.user.model.response.UserResponseDto;
import com.c202.user.user.model.response.UserWithSubscriptionDto;

import java.util.List;

public interface UserService {

    UserResponseDto getUserByUserSeq(Integer userSeq);

    UserResponseDto updateUser(Integer userSeq, UpdateUserRequestDto request);

    void deleteUser(Integer userSeq);

    UserResponseDto updateIntroduction(Integer userSeq, UpdateIntroductionDto introduction);

    String getUserBirthDate(Integer useSeq);

    List<UserProfileDto> getUserProfiles(List<Integer> userSeqList);

    UserResponseDto getRandomUser();

    UserWithSubscriptionDto getUserByUsernameWithSubscription(String username, Integer subscriberSeq);
    UserWithSubscriptionDto getUserByUserSeqWithSubscription(Integer userSeq, Integer subscriberSeq);

}