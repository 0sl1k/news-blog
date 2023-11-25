package com.example.demo.restController;

import com.example.demo.model.Post;
import com.example.demo.model.Repositiries.PostRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/secured")
public class PostController {
    @Autowired
    private PostRepo postRepo;

    @GetMapping("/posts")
    public List allPosts(){
        return postRepo.findAll();
    }
    @PostMapping("/createPost")
    public ResponseEntity<?> createPost(@RequestBody Post postDto){
        Post post = new Post();
        post.setTitle(postDto.getTitle());
        post.setDescription(postDto.getDescription());
        post.setText(postDto.getText());
        postRepo.save(post);
        return new ResponseEntity("Crated",HttpStatus.OK);

    }
}
