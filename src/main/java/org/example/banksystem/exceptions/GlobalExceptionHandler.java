package org.example.banksystem.exceptions;

import org.example.banksystem.dto.response.ApiResponseDTO;
import org.example.banksystem.exceptions.cards.CardAccessDeniedException;
import org.example.banksystem.exceptions.cards.CardNotFoundException;
import org.example.banksystem.exceptions.cards.CardNotInService;
import org.example.banksystem.exceptions.users.UserAccessDeniedException;
import org.example.banksystem.exceptions.users.UserNotFoundException;
import org.example.banksystem.exceptions.users.UserWrongCredentialsException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Глобальный обработчик исключений для REST API
 * <p>
 * Перехватывает исключения, возникающие в контроллерах и сервисах,
 * и преобразует их в стандартизированные HTTP ответы с использованием ApiResponseDTO.
 * Обеспечивает единообразную обработку ошибок во всем приложении.
 * </p>
 *
 * @author George
 * @version 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает исключения доступа к карте
     *
     * @param e исключение доступа к карте
     * @return ResponseEntity с HTTP статусом 403 (Forbidden)
     */
    @ExceptionHandler(CardAccessDeniedException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleCardAccessDeniedException(CardAccessDeniedException e) {
        return ResponseEntity.status(403).body(ApiResponseDTO.error(e.getMessage()));
    }

    /**
     * Обрабатывает исключения ненайденной карты
     *
     * @param e исключение ненайденной карты
     * @return ResponseEntity с HTTP статусом 404 (Not Found)
     */
    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleCardNotFoundException(CardNotFoundException e) {
        return ResponseEntity.status(404).body(ApiResponseDTO.error(e.getMessage()));
    }

    /**
     * Обрабатывает исключения недоступности карты для операций
     *
     * @param e исключение недоступности карты
     * @return ResponseEntity с HTTP статусом 400 (Bad Request)
     */
    @ExceptionHandler(CardNotInService.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleCardNotInServiceException(CardNotInService e) {
        return ResponseEntity.status(400).body(ApiResponseDTO.error(e.getMessage()));
    }

    /**
     * Обрабатывает исключения ненайденного пользователя
     *
     * @param e исключение ненайденного пользователя
     * @return ResponseEntity с HTTP статусом 404 (Not Found)
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleUserNotFoundException(UserNotFoundException e) {
        return ResponseEntity.status(404).body(ApiResponseDTO.error(e.getMessage()));
    }

    /**
     * Обрабатывает исключения доступа пользователя
     *
     * @param e исключение доступа пользователя
     * @return ResponseEntity с HTTP статусом 403 (Forbidden)
     */
    @ExceptionHandler(UserAccessDeniedException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleUserAccessDeniedException(UserAccessDeniedException e) {
        return ResponseEntity.status(403).body(ApiResponseDTO.error(e.getMessage()));
    }

    /**
     * Обрабатывает исключения неверных учетных данных пользователя
     *
     * @param e исключение неверных учетных данных
     * @return ResponseEntity с HTTP статусом 400 (Bad Request)
     */
    @ExceptionHandler(UserWrongCredentialsException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleUserWrongCredentialsException(UserWrongCredentialsException e) {
        return ResponseEntity.status(400).body(ApiResponseDTO.error(e.getMessage()));
    }
}