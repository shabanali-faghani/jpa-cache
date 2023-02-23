package ir.tata.jpacache.dto;

import ir.tata.jpacache.FollowUp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComplexType extends FollowUp implements Serializable {
    private int i;
    private String s;

    public enum RetType {
        NORMAL, NULL, EXCEPTION
    }
}
