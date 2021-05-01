package ru.ezhov.quick.action.infrastructure;

import org.junit.Test;
import ru.ezhov.quick.action.QuickActionRepositoryException;
import ru.ezhov.quick.action.contract.QuickAction;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class YmlQuickActionRepositoryTest {
    @Test
    public void actions() {
        try (InputStream is = this.getClass().getResourceAsStream("/actions.yml")) {
            YmlQuickActionRepository repository = new YmlQuickActionRepository(is);

            List<QuickAction> list = repository.actions();

            assertEquals(6, list.size());
        } catch (IOException | QuickActionRepositoryException e) {
            e.printStackTrace();
        }
    }
}