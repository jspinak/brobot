package com.brobot.app.database.repositories;

import com.brobot.app.database.entities.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface ImageRepo extends JpaRepository<ImageEntity, Long> {
    List<ImageEntity> findByName(String name);

}
