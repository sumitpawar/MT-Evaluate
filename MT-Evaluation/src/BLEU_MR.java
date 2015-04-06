import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class BLEU_MR {

  public static class BLEUMapper
       extends Mapper<Object, Text, Text, IntWritable>{

    private IntWritable val = new IntWritable();
    private Text textkey = new Text();


	private double getGramMatch(int gram, String h, String[] ref) {
		double match = 0.0;
		int len = ref.length - (gram - 1);
		for(int j = 0; j < len; j++) {
			String rfr = "";
			for(int i = gram; i > 0 ; i++) {
				rfr += ref[(i--)+j];
			}
			if(h.contains(rfr)) {
				match++;
			}
		}
		match = match / ((double) len);
		return match;
	}
	
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
    	String[] input = value.toString().split("\\s+\\|+\\s+");
  	  	String k = input[0];
  	  	String h1 = input[1];
  	  	String h2 = input[2];
  	  	String ref = input[3];
  	  	double h1len = h1.length();
		double h2len = h2.length();
		double reflen = ref.length();
		
		double bp1 = (h1len < reflen)?Math.exp(1 - (h1len / reflen)):1.0;
		double bp2 = (h2len < reflen)?Math.exp(1 - (h2len / reflen)):1.0;
		
		double t1 = 0.0, t2 = 0.0;
		for(int j = 0; j < 5; j++) {
			t1 += Math.log(getGramMatch(j, h1, ref.split("\\s+")) ) / reflen;
			t2 += Math.log(getGramMatch(j, h2, ref.split("\\s+")) ) / reflen;
		}
		
		double bleu1 = bp1 * Math.exp(t1);
		double bleu2 = bp2 * Math.exp(t2);
		
		int dataans = 0;
		if(bleu1 > bleu2) {
			dataans = 1;
		}else if(bleu1 < bleu2) {
			dataans = -1;
		}
		textkey.set(k);
		val.set(dataans);
		context.write(textkey, val);
    }
  }

  public static class BLEUReducer
       extends Reducer<Text,IntWritable,Text,IntWritable> {

    public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
                       ) throws IOException, InterruptedException {
    	/*int count = 0;
    	Iterator<IntWritable> vals = values.iterator();
    	while (vals.hasNext()) {
    		vals.next();
    		count++; 
    		if(count >= 2) {
    			context.write(key, null);
    			break;
    		}
    	}*/
    	Iterator<IntWritable> vals = values.iterator();
    	while (vals.hasNext()) {
    		context.write(key, (IntWritable) vals);
    	}
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "BLEU_MR");
    job.setJarByClass(BLEU_MR.class);
    job.setMapperClass(BLEUMapper.class);
    //job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(BLEUReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
