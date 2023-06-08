package cn.sleepybear.fileconvert.exception;

import java.io.Serial;

/**
 * There is description
 *
 * @author sleepybear
 * @date 2022/09/18 20:04
 */
public class FrontException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 2363333638031977753L;

    private final Integer type;

    public FrontException() {
        super();
        type = FrontExceptionConstant.TypeEnum.NORMAL.getType();
    }

    public FrontException(String message) {
        super(message);
        type = FrontExceptionConstant.TypeEnum.NORMAL.getType();
    }

    public FrontException(String message, Integer type) {
        super(message);
        this.type = type;
    }

    public Integer getType() {
        return type;
    }
}
