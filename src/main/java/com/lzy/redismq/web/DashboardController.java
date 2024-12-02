package com.lzy.redismq.web;

import com.lzy.redismq.config.RedisMQHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: TODO
 * @Author: liuziyao
 * @Date: 2024-08-06 下午 05:45
 **/
@Validated
@Controller
@RequestMapping("/redis-mq")
public class DashboardController{
    @Autowired
    RedisMQHelper redisMQHelper;


    @GetMapping("/streams")
    @ResponseBody
    public Object getStreams(){
        List<RedisMQHelper.StreamBaseInfo> streams = redisMQHelper.streams();
        return streams;
    }
    @GetMapping("/groups")
    @ResponseBody
    public Object getGroups(@NotBlank(message = "参数必填") String stream){
        StreamInfo.XInfoGroups groups = redisMQHelper.groups(stream);
        return groups.stream().collect(Collectors.toList());
    }
    @GetMapping("/consumers")
    @ResponseBody
    public Object getConsumers(@NotBlank(message = "参数必填") String stream,@NotBlank(message = "参数必填") String group){
        StreamInfo.XInfoConsumers consumers = redisMQHelper.consumers(stream, group);
        return consumers.stream().collect(Collectors.toList());
    }
    @GetMapping("/size")
    @ResponseBody
    public Object getQueueLen(@NotBlank(message = "参数必填") String stream){
        return redisMQHelper.size(stream);
    }
}
