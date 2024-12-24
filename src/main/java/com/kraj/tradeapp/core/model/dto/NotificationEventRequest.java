package com.kraj.tradeapp.core.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import javax.xml.bind.annotation.XmlMimeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEventRequest {

    @NotBlank(message = "Indicator is required")
    @Size(max = 50)
    private String indicator;

    @NotBlank(message = "Interval is required")
    @Size(max = 20)
    private String interval;
}
