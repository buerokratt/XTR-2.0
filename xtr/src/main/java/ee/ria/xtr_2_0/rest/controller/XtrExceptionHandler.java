package ee.ria.xtr_2_0.rest.controller;

import ee.ria.xtr_2_0.exception.XtrException;
import ee.ria.xtr_2_0.model.XtrErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Exception handler for rest endpoints. Has methods returning ResponsEntity which allows
 * writing error content to response body.
 */
@ControllerAdvice
@Slf4j
public class XtrExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * This methods handles specifically XtrExceptions
     * @param e exception being handled
     * @param request request during which the exception was thrown
     * @return ResponseEntity containing XtrErrorResponse
     *
     * @see XtrException
     * @see XtrErrorResponse
     */
    @ExceptionHandler(XtrException.class)
    public ResponseEntity<Object> handleXtrException(XtrException e, WebRequest request) {
        ResponseStatus status = AnnotatedElementUtils.findMergedAnnotation(e.getClass(), ResponseStatus.class);
        HttpStatus httpStatus = status != null ? status.value() : HttpStatus.INTERNAL_SERVER_ERROR;

        log.error("{}: {}", e.getClass().getName(), e.getData());

        return handleExceptionInternal(e, null, new HttpHeaders(), httpStatus, request);
    }

    /**
     * This method handles all other possible thrown Exceptions
     * @param e exception being handled
     * @param request request during which the exception was thrown
     * @return ResponseEntity containing XtrErrorResponse
     *
     * @see XtrErrorResponse
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception e, WebRequest request) {
        return handleExceptionInternal(e, null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception e, Object body, HttpHeaders headers,
                                                             HttpStatus status, WebRequest request) {
        log.error(String.valueOf(status), e);
        return new ResponseEntity<>(new XtrErrorResponse(status), headers, status);
    }
}
