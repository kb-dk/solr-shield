package dk.kb.template;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListManipulator {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    public List<String> reverseList(List<String> input) {
        log.debug("Input list was: {}", input);
        List<String> output = new ArrayList<>(); 
        for(int i = input.size() - 1; i >= 0; i--) {
            output.add(input.get(i));
        }
        log.debug("Output list is: {}", output);
        return output;
    }
    
}
