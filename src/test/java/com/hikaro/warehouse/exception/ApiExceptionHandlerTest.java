package com.hikaro.warehouse.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hikaro.warehouse.controller.CategoryController;
import com.hikaro.warehouse.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.MissingServletRequestParameterException;

@ExtendWith(MockitoExtension.class)
class ApiExceptionHandlerTest {

    @Mock
    private CategoryService categoryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new CategoryController(categoryService))
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void shouldReturnUnifiedValidationErrorResponse() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"description\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/categories"))
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[0].field").value("description"))
                .andExpect(jsonPath("$.errors[1].field").value("name"));
    }

    @Test
    void shouldReturnSafeDuplicateKeyMessageForIntegrityViolations() throws Exception {
        when(categoryService.create(any()))
                .thenThrow(new DataIntegrityViolationException(
                        "ERROR: duplicate key value violates unique constraint \"uk_categories_name\"\nDetail: Key (name)=(Hardware) already exists."
                ));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Hardware","description":"Stock"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Value 'Hardware' for field 'name' already exists"));
    }

    @Test
    void shouldReturnUnifiedNotFoundResponse() throws Exception {
        when(categoryService.getById(99L))
                .thenThrow(new ResourceNotFoundException("Category with id 99 not found"));

        mockMvc.perform(get("/api/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Category with id 99 not found"))
                .andExpect(jsonPath("$.path").value("/api/categories/99"))
                .andExpect(jsonPath("$.errors.length()").value(0));
    }

    @Test
    void shouldReturnOriginalIllegalArgumentMessageForBadRequest() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/products/search");

        ResponseEntity<ApiErrorResponse> response = handler.handleBadRequest(
                new IllegalArgumentException("name must not be blank"),
                request
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("name must not be blank", response.getBody().message());
        assertEquals("/api/products/search", response.getBody().path());
    }

    @Test
    void shouldFallbackToMostSpecificCauseMessageWhenDuplicatePatternIsAbsent() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/products");

        ResponseEntity<ApiErrorResponse> response = handler.handleBadRequest(
                new DataIntegrityViolationException("wrapper", new RuntimeException("constraint failed")),
                request
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("constraint failed", response.getBody().message());
    }

    @Test
    void shouldFallbackToExceptionMessageWhenMostSpecificCauseMessageIsNull() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/products");

        ResponseEntity<ApiErrorResponse> response = handler.handleBadRequest(
                new DataIntegrityViolationException("wrapper", new RuntimeException((String) null)),
                request
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("wrapper", response.getBody().message());
    }

    @Test
    void shouldSkipNullMessagesWhenSearchingDuplicateKeyCause() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/products");
        RuntimeException duplicateCause = new RuntimeException("ERROR: duplicate key value violates unique constraint. Detail: Key (sku)=(SKU-2000) already exists.");
        RuntimeException nullMessageCause = new RuntimeException((String) null, duplicateCause);

        ResponseEntity<ApiErrorResponse> response = handler.handleBadRequest(
                new DataIntegrityViolationException("wrapper", nullMessageCause),
                request
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Value 'SKU-2000' for field 'sku' already exists", response.getBody().message());
    }

    @Test
    void shouldReturnValidationErrorsForBindException() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/products");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "quantity", -1, false, null, null, "must be positive"));
        bindingResult.addError(new FieldError("request", "name", "", false, null, null, "must not be blank"));

        ResponseEntity<ApiErrorResponse> response = handler.handleBindException(
                new BindException(bindingResult),
                request
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().message());
        assertEquals("name", response.getBody().errors().getFirst().field());
        assertEquals("quantity", response.getBody().errors().get(1).field());
    }

    @Test
    void shouldReturnBadRequestForMalformedRequestBody() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/categories");

        ResponseEntity<ApiErrorResponse> response = handler.handleMalformedRequest(
                new HttpMessageNotReadableException("invalid json", new MockHttpInputMessage(new byte[0])),
                request
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Request body is invalid", response.getBody().message());
    }

    @Test
    void shouldReturnBadRequestForMissingRequestParameter() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/products/search");

        ResponseEntity<ApiErrorResponse> response = handler.handleMalformedRequest(
                new MissingServletRequestParameterException("name", "String"),
                request
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Request body is invalid", response.getBody().message());
    }

    @Test
    void shouldReturnInternalServerErrorForIllegalState() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/demo/with-transaction");

        ResponseEntity<ApiErrorResponse> response = handler.handleIllegalState(
                new IllegalStateException("boom"),
                request
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("boom", response.getBody().message());
    }

    @Test
    void shouldReturnGenericUnexpectedServerError() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/unknown");

        ResponseEntity<ApiErrorResponse> response = handler.handleUnexpectedException(
                new Exception("boom"),
                request
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected server error", response.getBody().message());
        assertEquals("/api/unknown", response.getBody().path());
    }
}
