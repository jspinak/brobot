package io.github.jspinak.brobot.app.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DatabaseCheckService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void checkForDuplicateSceneIds() {
        String query = "SELECT scene_id, COUNT(*) AS count FROM state_scenes GROUP BY scene_id HAVING COUNT(*) > 1";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(query);

        for (Map<String, Object> row : results) {
            Long sceneId = (Long) row.get("scene_id");
            Long count = (Long) row.get("count");
            System.out.println("Duplicate scene_id: " + sceneId + ", Count: " + count);
        }
    }
}

