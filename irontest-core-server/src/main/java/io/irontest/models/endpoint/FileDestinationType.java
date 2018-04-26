package io.irontest.models.endpoint;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by Zheng on 6/12/2017.
 */
public enum FileDestinationType {
    LOCAL_NETWORK("Local_Network"), FTP_SFTP("Ftp_Sftp");

    private final String text;

    FileDestinationType(String text) {
        this.text = text;
    }

    @Override
    @JsonValue
    public String toString() {
        return text;
    }

    public static FileDestinationType getByText(String text) {
        for (FileDestinationType e : values()) {
            if (e.text.equals(text)) {
                return e;
            }
        }
        return null;
    }
}
