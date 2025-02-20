package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.persistance.mongodb.MasterConfig;
import com.kraj.tradeapp.core.repository.mongodb.MasterConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MasterConfigService {

    private final MasterConfigRepository masterConfigRepository;

    public void seedMasterConfig() {
        masterConfigRepository.deleteAll();
        MasterConfig config = MasterConfig.builder()
            .allAutomationEnabled(true)
            .vivekAutomationEnabled(true)
            .krajAutomationEnabled(true)
            .build();
        masterConfigRepository.save(config);
    }

    public void enableAllAutomation() {
        masterConfigRepository
            .findAll()
            .forEach(masterConfig -> {
                masterConfig.setAllAutomationEnabled(true);
                masterConfigRepository.save(masterConfig);
            });
    }

    public void disableAllAutomation() {
        masterConfigRepository
            .findAll()
            .forEach(masterConfig -> {
                masterConfig.setAllAutomationEnabled(false);
                masterConfigRepository.save(masterConfig);
            });
    }

    public void enableVivekAutomation() {
        masterConfigRepository
            .findAll()
            .forEach(masterConfig -> {
                masterConfig.setVivekAutomationEnabled(true);
                masterConfigRepository.save(masterConfig);
            });
    }

    public void disableVivekAutomation() {
        masterConfigRepository
            .findAll()
            .forEach(masterConfig -> {
                masterConfig.setVivekAutomationEnabled(false);
                masterConfigRepository.save(masterConfig);
            });
    }

    public void enableKrajAutomation() {
        masterConfigRepository
            .findAll()
            .forEach(masterConfig -> {
                masterConfig.setKrajAutomationEnabled(true);
                masterConfigRepository.save(masterConfig);
            });
    }

    public void disableKrajAutomation() {
        masterConfigRepository
            .findAll()
            .forEach(masterConfig -> {
                masterConfig.setKrajAutomationEnabled(false);
                masterConfigRepository.save(masterConfig);
            });
    }

    public boolean isAllAutomationEnabled() {
        return masterConfigRepository.findAll().get(0).isAllAutomationEnabled();
    }
}
