package com.todo.web;

import com.todo.domain.Status;
import com.todo.domain.Task;
import com.todo.dao.TaskDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/")
public class TaskController {

    @Autowired
    TaskDAO taskDAO;

    @ModelAttribute
    public void populateStatuses(Model model) {
        model.addAttribute("allStatuses", Arrays.asList(Status.DONE, Status.PAUSED, Status.IN_PROGRESS));
    }

    @GetMapping("/")
    public String getAll(Model model) {
        List<Task> tasks = taskDAO.findAll();
        model.addAttribute("tasks", tasks);
        model.addAttribute("forCreate", new Task());
        return "hello";
    }

    @PostMapping("/tasks")
    public String create(@Valid Task forCreate, BindingResult result, Model model) {
        if (result.hasErrors()) {
            List<Task> tasks = taskDAO.findAll();
            model.addAttribute("tasks", tasks);
            model.addAttribute("forCreate", forCreate);
            model.addAttribute("result", result);
            return "hello";
        }
        taskDAO.save(forCreate);
        return "redirect:/";
    }

    @PostMapping("/tasks/{id}")
    public String delete(@PathVariable Integer id, Model model) {
        taskDAO.deleteById(id);
        List<Task> tasks = taskDAO.findAll();
        model.addAttribute("tasks", tasks);
        model.addAttribute("forCreate", new Task());
        return "redirect:/";
    }

}
