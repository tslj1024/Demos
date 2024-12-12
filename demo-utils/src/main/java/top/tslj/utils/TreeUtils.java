package top.tslj.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * 从扁平化数据转换为树形数据
 *
 * @author tslj1024@163.com
 * @since  1.0.0
 */
public class TreeUtils {

    public static class Entity {
        private String id;
        private String pid;
        private String name;
        List<Entity> children;
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getPid() {
            return pid;
        }
        
        public void setPid(String pid) {
            this.pid = pid;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public List<Entity> getChildren() {
            return children;
        }
        
        public void setChildren(List<Entity> children) {
            this.children = children;
        }
    }

    /*
    1. 数据库中查询出来的数据可能没有虚拟根
     */

    /**
     * 哈希表加速方式
     */
    public static Entity listToTree(List<Entity> list) {
        Entity root = new Entity();
        root.setId("0");

        Map<String, List<Entity>> map = list.stream().collect(Collectors.groupingBy(Entity::getPid));
        list.forEach(item -> item.setChildren(map.getOrDefault(item.getId(), new ArrayList<>())));
        root.setChildren(map.get(root.getId()));

        return root;
    }

    /**
     * 生成随机 Entity 列表
     * @return List<Entity>
     */
    public static List<Entity> getEntityList(int size) {
        List<Entity> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (i == 0) {
                continue;
            }
            Entity entity = new Entity();
            entity.setId(String.valueOf(i));
            entity.setPid(String.valueOf(i / 10));
            entity.setName("name" + i);
            list.add(entity);
        }
        return list;
    }

    /**
     * 测试
     */
    public static void main(String[] args) {
        int startCount = 0;
        int count = 1000;

        long total = 0L;

        for (int i = startCount; i < count; i++) {
            List<Entity> list1 = getEntityList(i);
            long start = System.currentTimeMillis();
            Entity root2 = listToTree(list1);
            if (i == 100) {
                System.out.println(root2);
            }
            long end = System.currentTimeMillis();
            total += end - start;
        }

        System.out.println("listToTree耗时：" + total + "ms"); // 127 ms
        // 1000 37ms
        // 10000 1250ms
    }

}
