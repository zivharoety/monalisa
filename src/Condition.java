import java.io.Serializable;

public interface Condition extends Serializable {
    public boolean check(Pic pic);

}