package com.igeeksky.xredis.examples;

import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * @author Patrick.Lau
 * @since 1.0.0
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public CompletableFuture<Response<User>> addUser(@RequestBody User user) {
        System.out.println("addUser:" + user);
        if (user == null) {
            return CompletableFuture.completedFuture(Response.error("user is null"));
        }
        return userService.addUser(user);
    }

    @GetMapping("/get/{id}")
    public CompletableFuture<Response<User>> getUser(@PathVariable("id") Long id) {
        System.out.println("getUser:" + id);
        if (id == null) {
            return CompletableFuture.completedFuture(Response.error("id is null"));
        }
        return userService.getUser(id);
    }

}
