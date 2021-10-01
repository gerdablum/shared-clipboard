package de.alina.clipboard.repo;

import de.alina.clipboard.model.DataType;
import de.alina.clipboard.model.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static de.alina.clipboard.repo.IDataManager.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DataManagerTest {

    private DataManager sut;

    @Mock
    private RedisTemplate<String, Object> redisTemplateMock;

    @Mock
    private ValueOperations<String, Object> opsValueMock;

    @Before
    public void setUp() throws Exception {
        when(redisTemplateMock.opsForValue()).thenReturn(opsValueMock);
        sut = new DataManager(redisTemplateMock);
    }

    @Test
    public void getData_returnsUserWithStringData() {

        UUID id = UUID.randomUUID();
        User expectedUser = defaultUser(id);
        when(redisTemplateMock.hasKey(any())).thenReturn(true);
        when(opsValueMock.get(USER_ID + id)).thenReturn(expectedUser.stringData);
        when(opsValueMock.get(TYPE + id)).thenReturn(expectedUser.type);

        User actualUser = sut.getData(id);

        assertEquals(expectedUser.stringData, actualUser.stringData);
        assertEquals(expectedUser.type, actualUser.type);

    }

    @Test
    public void getData_returnsUserWithFileData() {

        UUID id = UUID.randomUUID();
        User expectedUser = defaultUserWithFileData(id);
        when(redisTemplateMock.hasKey(any())).thenReturn(true);
        when(opsValueMock.get(USER_ID + id)).thenReturn(expectedUser.fileUrl);
        when(opsValueMock.get(TYPE + id)).thenReturn(expectedUser.type);

        User actualUser = sut.getData(id);

        assertEquals(expectedUser.fileUrl, actualUser.fileUrl);
        assertEquals(expectedUser.type, actualUser.type);

    }

    @Test
    public void saveData_savesUserWithExpiration() {

        UUID id = UUID.randomUUID();
        User expectedUser = defaultUser(id);
        when(redisTemplateMock.hasKey(any())).thenReturn(true);
        when(redisTemplateMock.getExpire(any())).thenReturn(1L);

        sut.saveData(expectedUser);

        verify(opsValueMock).set(USER_ID + id, expectedUser.stringData);
        verify(redisTemplateMock).expire(USER_ID + id, 1L, TimeUnit.SECONDS);


    }

    @Test
    public void deleteUser_deletesKeys() {
        UUID id = UUID.randomUUID();
        when(redisTemplateMock.hasKey(any())).thenReturn(true);

        boolean successful = sut.deleteUser(id);

        verify(redisTemplateMock).delete(USER_ID + id);
        verify(redisTemplateMock).delete(TYPE + id);
        assertTrue(successful);
    }

    @Test
    public void deleteUser_deletesStoredFiles() throws IOException {
        File testfile = new File(UPLOADED_FOLDER + "test");
        testfile.createNewFile();
        UUID id = UUID.randomUUID();
        when(redisTemplateMock.hasKey(any())).thenReturn(true);

        boolean successful = sut.deleteUser(id);

        verify(redisTemplateMock).delete(USER_ID + id);
        verify(redisTemplateMock).delete(TYPE + id);
        assertFalse(testfile.exists());
        assertTrue(successful);

    }

    @Test
    public void createUser_returnsUserAndPersist() {

    }

    @Test
    public void storeFile_savesFileWithCorrectPathname() {

    }

    @Test
    public void getStoredFileData_returnsFileDataWithCorrectPathname() {

    }

    @Test
    public void deleteFilesWithUserNonExisting_deletesCorrectFiles() {

    }

    private User defaultUser(UUID id) {

        User user = new User();
        user.id = id;
        user.stringData = "someString";
        user.type = DataType.STRING;

        return user;

    }

    private User defaultUserWithFileData(UUID id) {

        User user = new User(id);
        user.fileUrl = "fileUrl";
        user.type = DataType.FILE;

        return user;
    }
}