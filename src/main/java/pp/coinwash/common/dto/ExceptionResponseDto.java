package pp.coinwash.common.dto;

public record ExceptionResponseDto<T>(Integer status, String message, T data) {

    public static <T> ExceptionResponseDto<T> of(Integer status, String message, T data) {
        return new ExceptionResponseDto<>(status, message, data);
    }

    public static <T> ExceptionResponseDto<T> of(Integer status, String message) {
        return new ExceptionResponseDto<>(status, message, null);
    }
}