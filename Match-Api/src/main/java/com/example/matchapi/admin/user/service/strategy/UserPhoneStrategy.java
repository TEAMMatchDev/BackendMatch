package com.example.matchapi.admin.user.service.strategy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.example.matchapi.admin.user.enums.UserFilter;
import com.example.matchdomain.common.model.Status;
import com.example.matchdomain.user.adaptor.UserAdaptor;
import com.example.matchdomain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserPhoneStrategy implements UserStrategy{
	private final UserAdaptor userAdaptor;

	@Override
	public Page<UserRepository.UserList> getUserList(Pageable pageable, String content) {
		return userAdaptor.getUserListByPhone(pageable, content);
	}

	@Override
	public UserFilter getFilter() {
		return UserFilter.PHONE;
	}
}
