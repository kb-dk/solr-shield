package dk.kb.template;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ListManipulatorTest {
    
    @Tag("fast")
    @Test
    public void testFlipList() throws IOException {
        List<String> in = Arrays.asList("Hello", "World");
        
        ListManipulator lm = new ListManipulator();
        List<String> out = lm.reverseList(in);
        
        List<String> expectedReversedList = Arrays.asList("World", "Hello");
        assertEquals(expectedReversedList, out);
    }
 
}