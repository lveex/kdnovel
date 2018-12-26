package com.keduw.controller;

import com.keduw.jedis.JedisClient;
import com.keduw.model.Chapter;
import com.keduw.model.User;
import com.keduw.service.ChapterService;
import com.keduw.service.RecordService;
import com.keduw.util.Encoder;
import com.keduw.util.Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 章节信息
 */
@RestController
@RequestMapping("/chapter")
@PropertySource("classpath:cache.properties")
public class ChapterController {

    @Autowired
    private ChapterService chapterService;
    @Autowired
    private RecordService recordService;
    @Autowired
    private JedisClient jedisClient;
    @Value("user_ip")
    private String session;
    @Value("catagory_record")
    private String record;

    @RequestMapping("/list/{id}")
    public List<Chapter> infoList(@PathVariable("id") String id){
        int novelId = Parser.parserInt(id, 0);
        List<Chapter> chapters = new ArrayList<>();
        if(novelId > 0){
            chapters = chapterService.getChapterList(novelId);
        }
        return chapters;
    }

    //获取章节的内容
    @Transactional
    @RequestMapping("/content/{novel}/{chapter}")
    public String getContent(HttpServletRequest request, @PathVariable("novel") String novel, @PathVariable("chapter") String chapter){
        int novelId = Parser.parserInt(novel, 1);
        int chapterId = Parser.parserInt(chapter, 1);
        User user = (User)request.getSession().getAttribute(session);
        if(user != null){
            int userId = user.getId();
            if(chapterId == 1){
                //刚进入页面，查询阅读记录
                chapterId = recordService.getUserRecord(userId, novelId);
            }else{
                //阅读记录发送变化
                StringBuilder builder = new StringBuilder();
                builder.append("record");
                builder.append(userId);
                builder.append(novelId);
                builder.append(chapterId);
                jedisClient.hset(record, builder.toString(), String.valueOf(chapterId));
            }
        }
        return chapterService.getChapterContent(novelId, chapterId);
    }

    //获取章节标题和内容
    @Transactional
    @RequestMapping("/info/{novel}/{chapter}")
    public Chapter getBaseInfo(HttpServletRequest request, @PathVariable("novel") String novel, @PathVariable("chapter") String chapter){
        int novelId = Parser.parserInt(novel, 1);
        int chapterId = Parser.parserInt(chapter, 1);
        User user = (User)request.getSession().getAttribute(session);
        if(user != null){
            int userId = user.getId();
            if(chapterId == 1){
                //刚进入页面，查询阅读记录
                chapterId = recordService.getUserRecord(userId, novelId);
            }else{
                //阅读记录发送变化
                StringBuilder builder = new StringBuilder();
                builder.append("record");
                builder.append(userId);
                builder.append(novelId);
                builder.append(chapterId);
                jedisClient.hset(record, builder.toString(), String.valueOf(chapterId));
            }
        }
        String content = chapterService.getChapterContent(novelId, chapterId);
        List<Chapter> infoList = chapterService.getChapterList(novelId);
        String name = "";
        for(Chapter info : infoList){
            if(info.getId() == chapterId){
                name = info.getName();
                break;
            }
        }
        Chapter info = new Chapter();
        info.setContent(Encoder.decodeHtml(content));
        info.setName(Encoder.decodeHtml(name));
        return info;
    }
}
