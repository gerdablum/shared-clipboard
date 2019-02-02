package de.alina.clipboard;

import de.alina.clipboard.exception.UnauthorizedException;
import de.alina.clipboard.model.User;
import de.alina.clipboard.repo.DataManager;
import de.alina.clipboard.repo.IDataManager;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.UUID;


@RestController
public class ClipboardRestController {

    private Logger logger = LoggerFactory.getLogger(ClipboardRestController.class);
    private final IDataManager database;

    @Autowired
    public ClipboardRestController(DataManager database) {
        super();
        this.database = database;
    }

    /**
     * returns an QR image which contains an id for user authentication. The user scans
     * the QR code with their smartphone. So client and server share the same id.
     * @param id user id
     * @return qr image
     */
    @RequestMapping(value = "/qr-image.png", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getQRImage(@RequestParam(value = "id") String id) {
        if (isInputInvalid(id)) {
            throw new UnauthorizedException();
        }
        ByteArrayOutputStream stream = QRCode.from(id).to(ImageType.PNG).stream();
        byte[] rawImage = stream.toByteArray();
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rawImage;
    }

    /**
     * Is called on every page load if no id is stored in cookies. Creates a new user id with 10 minutes
     * validation
     * @return User object which contains id
     */
    @RequestMapping(value = "/get-id")
    public User getID() {
        User u = new User(UUID.randomUUID());
        database.createUser(u.id);
        logger.debug("created user with id: " + u.id.toString());
        return u;
    }

    /**
     * receives data the user sends to the server and stores it for 10 minutes
     * @param id the users id
     * @param data which was sent to the server
     * @return
     */
    @RequestMapping(value = "/send-data", method = RequestMethod.POST)
    public String sendData(@RequestHeader(value = "id") String id,
                           @RequestHeader(value = "data") String data) {
        if (isInputInvalid(id)) {
            throw new UnauthorizedException();
        }
        User user = new User();
        user.id = UUID.fromString(id);
        user.stringData = data;
        database.saveStringData(user);
        // TODO return value
        return "sucessfull";
    }

    /**
     * gets the stored data for a specific user
     * @param id unique user id
     * @return stored data or unauthorized exception
     */
    @RequestMapping(value = "/get-data")
    public User getData(@RequestParam(value = "id") String id) {
        User u = new User();
        if (isInputInvalid(id)) {
            throw new UnauthorizedException();
        }
        UUID uuid = UUID.fromString(id);
        u.id = uuid;
        u.stringData = database.getStringData(uuid);
        return u;
    }

    private boolean isInputInvalid(String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (Exception e) {
            return true;
        }
        return !database.isUserExisting(uuid);
    }
}
