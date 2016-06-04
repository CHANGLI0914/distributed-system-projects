import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.util.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;

public class WordCountPercent {

  public static class TokenizerMapper
       extends Mapper<Object, Text, Text, IntWritable>{

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString());
      StringTokenizer itr2 = new StringTokenizer(value.toString());
       if(itr2.hasMoreTokens()){
      itr2.nextToken();
      while (itr2.hasMoreTokens()) {
        word.set(itr.nextToken()+" "+itr2.nextToken());
        context.write(word, one);
      }
    }
    }
  }

    public static class TopNReducer
       extends Reducer<Text,IntWritable,Text,IntWritable> {
    private IntWritable result = new IntWritable();
    private Integer totalnumber=0;
    private Text word = new Text();
    private Map<Text,IntWritable> Counter=new HashMap<Text,IntWritable>();

    public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
                       ) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      Counter.put(new Text(key),new IntWritable(sum));  
      totalnumber+=sum;
    }
    public void cleanup(Context context) throws IOException,InterruptedException{
      List list = new LinkedList(Counter.entrySet());
      Integer target=totalnumber/10;
      Collections.sort(list, new Comparator() {
          public int compare(Object o1, Object o2) {
               return ((Comparable) ((Map.Entry) (o2)).getValue())
              .compareTo(((Map.Entry) (o1)).getValue());
          }
     });
      int i=0;
      for (Iterator it = list.iterator(); it.hasNext();) {
         if(i>=target)
            break;
          Map.Entry<Text,IntWritable> entry = (Map.Entry)it.next();

          context.write(entry.getKey(), entry.getValue());
          i+=entry.getValue().get();
      }
    }
  }



  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "word count");

    job.setJarByClass(WordCountPercent.class);
    job.setMapperClass(TokenizerMapper.class);
    //job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(TopNReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));

    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}