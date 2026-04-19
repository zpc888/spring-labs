package prot.cxf.plugin;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ClientGenConfigTest {

    @Test
    void getSeiAnnotations_nullSei_returnsEmptyList() {
        ClientGenConfig config = new ClientGenConfig();
        assertEquals(List.of(), config.getSeiAnnotations());
    }

    @Test
    void getSeiAnnotations_seiWithAnnotations_returnsList() {
        ClientGenConfig.Sei sei = new ClientGenConfig.Sei();
        sei.setAnnotations(List.of("com.example.A", "com.example.B"));

        ClientGenConfig config = new ClientGenConfig();
        config.setSei(sei);

        assertEquals(List.of("com.example.A", "com.example.B"), config.getSeiAnnotations());
    }

    @Test
    void getSeiAnnotations_seiNullAnnotations_returnsEmptyList() {
        ClientGenConfig.Sei sei = new ClientGenConfig.Sei();
        sei.setAnnotations(null);

        ClientGenConfig config = new ClientGenConfig();
        config.setSei(sei);

        assertEquals(List.of(), config.getSeiAnnotations());
    }

    @Test
    void getOperations_null_returnsEmptyMap() {
        ClientGenConfig config = new ClientGenConfig();
        assertEquals(Map.of(), config.getOperations());
    }

    @Test
    void getOperations_withEntries_returnsMap() {
        Map<String, List<String>> ops = Map.of("ping", List.of("com.example.PingHandler"));

        ClientGenConfig config = new ClientGenConfig();
        config.setOperations(ops);

        assertEquals(1, config.getOperations().size());
        assertTrue(config.getOperations().containsKey("ping"));
        assertTrue(config.getOperations().get("ping").contains("com.example.PingHandler"));
    }

    @Test
    void hasSeiAnnotations_whenPresent_isTrue() {
        ClientGenConfig.Sei sei = new ClientGenConfig.Sei();
        sei.setAnnotations(List.of("com.example.A"));

        ClientGenConfig config = new ClientGenConfig();
        config.setSei(sei);

        assertFalse(config.getSeiAnnotations().isEmpty());
    }
}

