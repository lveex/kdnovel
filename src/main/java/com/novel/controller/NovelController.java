package com.novel.controller;

import com.novel.crawler.ChapterCrawler;
import com.novel.crawler.NovelCrawler;
import com.novel.crawler.UrlCrawler;
import com.novel.model.Chapter;
import com.novel.model.Novel;
import com.novel.service.serviceImpl.ChapterServiceImpl;
import com.novel.service.serviceImpl.NovelServiceImpl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ProjectName: novelSpider
 * @Package: com.novel.controller
 * @ClassName: TestController
 * @Description: java类作用描述:先爬去同一类别的小说URL，然后根据小说URL做种子，爬去
 *                  详细信息以及章节列表，然后把详细信息存入数据库，根据章节列表以及
 *                  小说ID爬取主要内容
 * @Author: 林浩东
 * @CreateDate: 2018/8/11/011 16:12
 * @UpdateUser: 林浩东
 * @UpdateDate: 2018/8/11/011 16:12
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
@RestController
@RequestMapping("/Novel")
public class NovelController {
    @Resource
    private NovelServiceImpl novelServiceImpl;
    @Resource
    private ChapterServiceImpl chapterServiceImpl;
    @RequestMapping("/NovelInfo")
    public String NovelInfo(){
            UrlCrawler urlCrawler = new UrlCrawler("urlCrawler",true);

        try {
            urlCrawler.start(2);
            while (urlCrawler.isResumable()){
                System.out.println(".....");
            }
            System.out.println("url爬取结束");
            List<String> urlList = urlCrawler.getUrlLists();
            //List<String> urlList =new ArrayList<>();
            //String tmp1 ="https://www.biquge5.com/28_28951/";
            //String tmp2 ="https://www.biquge5.com/30_30278/";
            //urlList.add(tmp1);
            //urlList.add(tmp2);
            NovelCrawler novelCrawler = new NovelCrawler("novelCrawler",false);
            for (String url:urlList
                 ) {
                novelCrawler.addSeed(url);
            }
            novelCrawler.start(1);
            while (novelCrawler.isResumable()){
                System.out.println(".....");
            }
            System.out.println("novel爬取结束");
            List<Novel> novelsList= novelCrawler.getNovelsList();
            /*
            * 将小说详细信息插入数据库
            * */
            for (Novel novel:novelsList
                    ) {
                novelServiceImpl.insertNovel(novel);
            }
            /*
            *
            * 将章节URL和小说id插入数据库
            * */
            List<Chapter> chapterList = novelCrawler.getChapterList();
            for (Chapter chapter:chapterList
                 ) {
                chapterServiceImpl.insertChapter(chapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "NovelInfo";
    }

    @RequestMapping("/ChapterContent")
    public String ChapterContent(){
        // List<Chapter> chapters = chapterServiceImpl.findByNovelIdChapter(Long.parseLong("1534051213252"));
        List<Long> novelIs = novelServiceImpl.findAllNovelId();
        for (Long novelId:novelIs
             ) {
            List<Chapter> chapters = chapterServiceImpl.findByNovelIdChapter(novelId);
            for (Chapter chapter:chapters
                 ) {
                ChapterCrawler chapterCrawler = new ChapterCrawler("chapter",false,chapter);
                try {
                    chapterCrawler.start(1);
                    while (chapterCrawler.isResumable()){
                    }
                    chapter = chapterCrawler.getChapter();
                    chapterServiceImpl.updateChapter(chapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("ChapterContent");
     return "ChapterContent";
    }

}
