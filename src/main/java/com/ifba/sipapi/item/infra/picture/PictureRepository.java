package com.ifba.sipapi.item.infra.picture;

import com.ifba.sipapi.item.domain.picture.Picture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PictureRepository extends JpaRepository<Picture, Long> {}
