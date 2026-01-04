package com.project.financeapi.service;

import com.project.financeapi.dto.ResponseDefaultDTO;
import com.project.financeapi.dto.operationGroup.OperationGroupCreateRequestDTO;
import com.project.financeapi.dto.operationGroup.OperationGroupResponseDTO;
import com.project.financeapi.dto.operationGroup.UpdateRequestOperationGroup;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.entity.OperationGroup;
import com.project.financeapi.entity.User;
import com.project.financeapi.enums.OperationStatus;
import com.project.financeapi.exception.AccessBlockedException;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.OperationGroupRepository;
import com.project.financeapi.repository.UserRepository;
import com.project.financeapi.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OperationGroupService {

    private final OperationGroupRepository operationGroupRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;


    @Transactional
    public OperationGroupResponseDTO create(String token, @NotNull OperationGroupCreateRequestDTO dto) {

        User user = getUser(token);

        if(operationGroupRepository.nameExitsByCreatedBy(user, dto.name())){
            throw new BusinessException(HttpStatus.CONFLICT, "Já exite um grupo de operação com esse nome.");
        }

        OperationGroup operationGroup = operationGroupRepository.save(
                new OperationGroup(dto.name(), user)
        );


        return operationGroup.toResponse();
    }

    @Transactional
    public ResponseDefaultDTO update(String token, UUID id, @NotNull UpdateRequestOperationGroup dto) {

        User user = getUser(token);

        OperationGroup operationGroup = operationGroupRepository.findByCreatedByById(user, id).orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND, "O grupo de operação não foi localizada."
        ));

        if(!operationGroup.getName().equalsIgnoreCase(dto.name())) {
            if (operationGroupRepository.nameExitsByCreatedBy(user, dto.name())) {
                throw new BusinessException(HttpStatus.CONFLICT, "Já exite um grupo de operação com esse nome.");
            }
        }

        if(operationGroup.getIsGlobal()){
            throw new AccessBlockedException("Você não tem permissão para editar este grupo de operação.");
        }

        operationGroup.setName(dto.name());

        operationGroupRepository.save(operationGroup);

        return new ResponseDefaultDTO("Grupo de operação editado com sucesso");
    }

    @Transactional
    public void updateStatusOperationGroup(String token, UUID id) {

        User user = getUser(token);


        OperationGroup operationGroup = operationGroupRepository.findByCreatedByById(user, id).orElseThrow(() -> new BusinessException(
                HttpStatus.NOT_FOUND, "O grupo de operação não foi localizada."
        ));

        if(operationGroup.getIsGlobal()){
            throw new AccessBlockedException("Você não tem permissão para desativar este grupo de operação.");
        }

        operationGroup.setOperationStatus(operationGroup.getOperationStatus().toggle());

        operationGroupRepository.save(operationGroup);

    }

    public List<OperationGroupResponseDTO> findAll(String token) {

        User user = getUser(token);

        return operationGroupRepository.findAllByUserOrDefault(user).stream().map(OperationGroup::toResponse).toList();
    }

    public List<OperationGroupResponseDTO> findAllOperationStatus(String token, OperationStatus operationStatus) {

        return operationGroupRepository.findAllByUserOperationStatus(getUser(token), operationStatus)
                .stream().map(OperationGroup::toResponse).toList();
    }

    public OperationGroupResponseDTO findById(String token, UUID id) {

        User user = getUser(token);

        OperationGroup operationGroup = operationGroupRepository.findByCreatedByById(user, id).orElseThrow(
                () -> new BusinessException(
                        HttpStatus.BAD_REQUEST, "O grupo de operação não foi encontrado"
                )
        );

        return operationGroup.toResponse();
    }

    private User getUser(String token) {
        JwtPayload payload = jwtUtil.extractPayload(token);

        return userRepository.findById(payload.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "O usuário informado não existe"));
    }

}
