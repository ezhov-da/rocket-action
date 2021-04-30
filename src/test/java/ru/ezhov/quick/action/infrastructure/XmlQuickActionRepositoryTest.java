package ru.ezhov.quick.action.infrastructure;


import org.junit.Test;
import ru.ezhov.quick.action.QuickAction;
import ru.ezhov.quick.action.QuickActionRepositoryException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class XmlQuickActionRepositoryTest {

    @Test
    public void actions() {
        try (InputStream is = this.getClass().getResourceAsStream("/actions.xml")) {
            XmlQuickActionRepository repository = new XmlQuickActionRepository(is);

            List<QuickAction> list = repository.actions();

            assertEquals(6, list.size());
        } catch (IOException | QuickActionRepositoryException e) {
            e.printStackTrace();
        }
    }
}