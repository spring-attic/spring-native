package org.springframework.samples.petclinic.owner;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link PetTypeFormatter}
 *
 * @author Colin But
 */
public class PetTypeFormatterTests {

    private PetRepository pets;

    private PetTypeFormatter petTypeFormatter;

    @BeforeEach
    void setup() {
        pets = new PetRepository() {
            @Override
            public List<PetType> findPetTypes() {
                return makePetTypes();
            }

            @Override
            public Pet findById(int id) {
                return null;
            }

            @Override
            public void save(Pet pet) {
            }
        };
        this.petTypeFormatter = new PetTypeFormatter(pets);
    }

    @Test
    public void testPrint() {
        PetType petType = new PetType();
        petType.setName("Hamster");
        String petTypeName = this.petTypeFormatter.print(petType, Locale.ENGLISH);
        assertEquals("Hamster", petTypeName);
    }

    @Test
    public void shouldParse() throws ParseException {
        PetType petType = petTypeFormatter.parse("Bird", Locale.ENGLISH);
        assertEquals("Bird", petType.getName());
    }

    @Test
    public void shouldThrowParseException() throws ParseException {
        assertThrows(ParseException.class,
                () -> petTypeFormatter.parse("Fish", Locale.ENGLISH));
    }

    /**
     * Helper method to produce some sample pet types just for test purpose
     *
     * @return {@link Collection} of {@link PetType}
     */
    private List<PetType> makePetTypes() {
        List<PetType> petTypes = new ArrayList<>();
        petTypes.add(new PetType() {
            {
                setName("Dog");
            }
        });
        petTypes.add(new PetType() {
            {
                setName("Bird");
            }
        });
        return petTypes;
    }

}
