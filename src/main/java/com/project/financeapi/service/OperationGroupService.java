package com.project.financeapi.service;

import com.project.financeapi.dto.operationGroup.OperationGroupCreateRequestDTO;
import com.project.financeapi.dto.operationGroup.OperationGroupResponseDTO;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.OperationGroup;
import com.project.financeapi.entity.User;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.OperationGroupRepository;
import com.project.financeapi.repository.UserRepository;
import com.project.financeapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class OperationGroupService {

    private final OperationGroupRepository operationGroupRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public OperationGroupService(OperationGroupRepository operationGroupRepository, UserRepository userRepository, JwtUtil jwtUtil) {
        this.operationGroupRepository = operationGroupRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public OperationGroupResponseDTO create(String token, OperationGroupCreateRequestDTO dto) {

        JwtPayload payload = jwtUtil.extractPayload(token);

        User user = userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "O usuário informado não existe"));

        OperationGroup operationGroup = operationGroupRepository.save(
                new OperationGroup(dto.name(), user)
        );

        return new OperationGroupResponseDTO(
                operationGroup.getId(),
                operationGroup.getName()
        );
    }
}
