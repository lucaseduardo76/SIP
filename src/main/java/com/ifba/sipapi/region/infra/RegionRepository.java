package com.ifba.sipapi.region.infra;

import com.ifba.sipapi.region.domain.RegionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends JpaRepository<RegionModel, Long> {}
