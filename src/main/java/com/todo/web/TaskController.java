package com.todo.web;

import com.todo.dao.TaskDAO;
import com.todo.domain.Status;
import com.todo.domain.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/")
public class TaskController {

    @Autowired
    private TaskDAO taskDAO;

    @Value("${page.size}")
    private Integer pageSize;

    @ModelAttribute
    public void populateStatuses(Model model) {
        model.addAttribute("allStatuses", Arrays.asList(Status.DONE, Status.PAUSED, Status.IN_PROGRESS));
    }

    @GetMapping("/")
    public String getAll(Model model, @RequestParam(required = false) Integer pageNum) {
        pageNum = Objects.isNull(pageNum) ? 0 : pageNum;
        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNum);
        Page<Task> page = taskDAO.findAll(pageable);
        Integer pagesCount = page.getTotalPages();
        List<Task> tasks = page.toList();
        model.addAttribute("pagesCount", pagesCount);
        model.addAttribute("currentPageNum", pageNum);
        model.addAttribute("tasks", tasks);
        model.addAttribute("task", new Task());
        return "main";
    }

    @PostMapping("/tasks/{id}")
    public String delete(@PathVariable Integer id) {
        taskDAO.deleteById(id);
        return "redirect:/";
    }

    @GetMapping("/tasks")
    public String getForm(@RequestParam(required = false) Integer modifiedTaskId, Model model) {
        Task task;
        if (Objects.isNull(modifiedTaskId)) {
            task = new Task();
        } else {
            task = taskDAO.findById(modifiedTaskId).orElseThrow(
                    () -> new EmptyResultDataAccessException("Task not found", 1));
        }
        model.addAttribute("task", task);
        return "form";
    }

    @PostMapping("/tasks")
    public String processForm(@Valid Task task, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("task", task);
            return "form";
        }
        taskDAO.save(task);
        return "redirect:/";
    }
}
