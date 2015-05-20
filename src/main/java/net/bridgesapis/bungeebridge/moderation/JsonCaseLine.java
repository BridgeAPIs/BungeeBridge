package net.bridgesapis.bungeebridge.moderation;

public class JsonCaseLine {
    private String addedBy = null;
    private String type = null;
    private String motif = null;
    private String duration = null;
    private Long timestamp = null;
	private Long durationTime = null;
	private boolean grave = false;

    public JsonCaseLine() {

    }

	public void setGrave() {
		grave = true;
	}

	public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

	public Long getDurationTime() {
		return durationTime;
	}

	public void setDurationTime(Long durationTime) {
		this.durationTime = durationTime;
	}
}
