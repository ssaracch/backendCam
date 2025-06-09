package com.projet.cameraproject.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.projet.cameraproject.entity.AlertsHistory;
import com.projet.cameraproject.entity.AlertsHistoryId;

public interface AlertsHistoryRepository extends JpaRepository<AlertsHistory, AlertsHistoryId> {}

