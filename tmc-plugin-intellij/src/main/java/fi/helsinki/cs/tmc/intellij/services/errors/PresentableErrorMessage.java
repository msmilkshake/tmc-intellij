package fi.helsinki.cs.tmc.intellij.services.errors;

import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.intellij.holders.TmcSettingsManager;

import com.intellij.notification.NotificationType;

class PresentableErrorMessage {
    private final String message;
    private final NotificationType messageType;

    private PresentableErrorMessage(String message, NotificationType messageType) {
        this.message = message;
        this.messageType = messageType;
    }

    String getMessage() {
        return message;
    }

    NotificationType getMessageType() {
        return messageType;
    }

    /**
     * Generates a human readable error message for a {@link TmcCoreException} and decides the
     * error's severity.
     */
    static PresentableErrorMessage forTmcException(TmcCoreException exception) {
        String causeMessage;

        causeMessage = exception.getMessage();

        String shownMessage;
        NotificationType type = NotificationType.WARNING;

        if (causeMessage.contains("Download failed")
                || causeMessage.contains("404")
                || causeMessage.contains("500")) {
            shownMessage = notifyAboutCourseServerAddressAndInternet();
        } else if (!TmcSettingsManager.get().userDataExists()) {
            shownMessage = notifyAboutUsernamePasswordAndServerAddress(causeMessage);
        } else if (causeMessage.contains("401")) {
            shownMessage = notifyAboutIncorrectUsernameOrPassword(causeMessage);
            type = NotificationType.ERROR;
        } else if (causeMessage.contains("Organization not selected")) {
            shownMessage = causeMessage;
        } else if (causeMessage.contains("Failed to fetch courses from the server")
                || causeMessage.contains("Failed to compress project")
                || causeMessage.contains("Failed to submit exercise")) {
            shownMessage = notifyAboutFailedSubmissionAttempt();
        } else if (TmcSettingsManager.get().getServerAddress().isEmpty()) {
            shownMessage = notifyAboutEmptyServerAddress(causeMessage);
        } else {
            shownMessage = causeMessage;
            type = NotificationType.ERROR;
        }

        return new PresentableErrorMessage(shownMessage, type);
    }

    private static String notifyAboutCourseServerAddressAndInternet() {
        return "Failed to download courses\n"
                + "Check that you have the correct course and server address\n"
                + "and you are connected to Internet";
    }

    private static String notifyAboutUsernamePasswordAndServerAddress(String causeMessage) {
        return causeMessage + "\nSet up your username, password and TMC server address.";
    }

    private static String notifyAboutEmptyServerAddress(String causeMessage) {
        return causeMessage
                + ".\nYou need to set up TMC server address "
                + "to be able to download and submit exercises.";
    }

    private static String notifyAboutFailedSubmissionAttempt() {
        return "Failed to submit exercise.\nPlease check your Internet connection";
    }

    private static String notifyAboutIncorrectUsernameOrPassword(String causeMessage) {
        return causeMessage + ".\nTMC Username or Password incorrect.";
    }
}
