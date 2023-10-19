package com.project.user.service;

import com.project.order.model.OrderDTO;
import com.project.order.service.OrderService;
import com.project.user.model.Role;
import com.project.user.model.User;
import com.project.user.model.UserDTO;
import com.project.user.repository.UserRepository;
import com.project.utils.exceptionhandler.exceptions.NoSuchElemException;
import com.project.utils.mappers.EntityDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;

import static com.project.utils.exceptionhandler.ExceptionMessages.FORBIDDEN;
import static com.project.utils.exceptionhandler.ExceptionMessages.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserService implements CrudUserService{
    private final UserRepository userRepository;
    private final EntityDtoMapper entityDtoMapper;
    private final OrderService orderService;

    @Override
    public Page<UserDTO> getAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(entityDtoMapper::toUserDTO);
    }

    @Override
    public UserDTO get(Long id) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (currentUser.getRole().equals(Role.ADMIN) || id.equals(currentUser.getId())) {
            return entityDtoMapper.toUserDTO(userRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElemException(MessageFormat.format(USER_NOT_FOUND, id))
                    ));
        }

        throw new AccessDeniedException(FORBIDDEN);
    }

    public Page<OrderDTO> getOrders(Long id, Pageable pageable) {
        return orderService.getByUser(id, pageable);
    }
}