package io.irontest.models.endpoint;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by rmason on 18/04/2018.
 */
public enum FileTransferProtocol {
    FTP("FTP"), FTPS("FTPS"), SFTP("SFTP");

    private final String text;

    FileTransferProtocol(String text) {
        this.text = text;
    }

    @Override
    @JsonValue
    public String toString() {
        return text;
    }

    public static FileTransferProtocol getByText(String text) {
        for (FileTransferProtocol e : values()) {
            if (e.text.equals(text)) {
                return e;
            }
        }
        return null;
    }
}
