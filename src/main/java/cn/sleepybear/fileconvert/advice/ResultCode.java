package cn.sleepybear.fileconvert.advice;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author XJX
 * @date 2021/8/10 0:33
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultCode<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = -2938642554402365880L;

    private Integer code;
    private String message;
    private Long time;
    private T result;

    public ResultCode(Integer code, String message, Long time, T result) {
        this.code = code;
        this.message = message;
        this.time = time;
        this.result = result;
    }

    public ResultCode(Integer code, String message, T result) {
        this(code, message, System.currentTimeMillis(), result);
    }

    public ResultCode(T result) {
        this(ResultCodeConstant.CodeEnum.SUCCESS.getCode(), null, result);
    }

    public ResultCode(Integer code, String message) {
        this(code, message, null);
    }

    public ResultCode(String message) {
        this(ResultCodeConstant.CodeEnum.COMMON_ERROR.getCode(), message);
    }

    public static ResultCode<String> buildMsg(String s) {
        return new ResultCode<>(ResultCodeConstant.CodeEnum.SUCCESS.getCode(), s);
    }

    public static ResultCode<String> buildResult(String s) {
        ResultCode<String> resultCode = new ResultCode<>(ResultCodeConstant.CodeEnum.SUCCESS.getCode(), null);
        resultCode.setResult(s);
        return resultCode;
    }
}
