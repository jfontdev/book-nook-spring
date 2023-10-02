package com.jordi.booknook.events;

import com.jordi.booknook.models.ShelfEntity;
import com.jordi.booknook.models.UserEntity;
import com.jordi.booknook.repositories.ShelfRepository;
import jakarta.persistence.PostPersist;
import org.springframework.context.annotation.Lazy;

import java.util.List;

public class UserEntityListener {
    private ShelfRepository shelfRepository;

    public UserEntityListener(@Lazy ShelfRepository shelfRepository) {
        this.shelfRepository = shelfRepository;
    }

    public UserEntityListener() {

    }

    @PostPersist
    public void afterUserCreated(UserEntity user){
            ShelfEntity shelf1 = new ShelfEntity(user,"Leido","image1.jpg",
                    "Libros que ya he leido.",true);
            ShelfEntity shelf2 = new ShelfEntity(user,"Quiero Leer","image2.jpg",
                    "Libros que quiero leer.",true);
            ShelfEntity shelf3 = new ShelfEntity(user,"Leyendo","image3.jpg",
                    "Libros que estoy leyendo.",true);

            shelfRepository.saveAll(List.of(shelf1,shelf2,shelf3));
        }
    }