package org.practice.eventticketingapi.event.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateEventRequest {
    @NotBlank
    private String name;

    @NotNull
    @Min(1)
    private Integer totalTickets;

    public CreateEventRequest() {}

    public CreateEventRequest(String name, Integer totalTickets) {
        this.name = name;
        this.totalTickets = totalTickets;
    }

    public String getName() {
        return name;
    }

    public Integer getTotalTickets() {
        return totalTickets;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTotalTickets(Integer totalTickets) {
        this.totalTickets = totalTickets;
    }
}
