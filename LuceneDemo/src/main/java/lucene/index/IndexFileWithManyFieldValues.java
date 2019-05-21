package lucene.index;

import io.FileOperation;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Random;

/**
 * @author Lu Xugang
 * @date 2019-02-21 09:58
 */
public class IndexFileWithManyFieldValues {
  private Directory directory;

  {
    try {
      FileOperation.deleteFile("./data");
      directory = new MMapDirectory(Paths.get("./data"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Analyzer analyzer = new WhitespaceAnalyzer();
  private IndexWriterConfig conf = new IndexWriterConfig(analyzer);
  private IndexWriter indexWriter;

  public void doIndex() throws Exception {

    FieldType type = new FieldType();
    type.setStored(true);
    type.setStoreTermVectors(true);
    type.setStoreTermVectorPositions(true);
    type.setStoreTermVectorPayloads(true);
    type.setStoreTermVectorOffsets(true);
    type.setTokenized(true);
    type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);

    conf.setUseCompoundFile(false);
    TieredMergePolicy policy = new TieredMergePolicy();
//    MergePolicy policy = new LogDocMergePolicy();
    conf.setMergePolicy(policy);
    conf.setMergeScheduler(new SerialMergeScheduler());
    indexWriter = new IndexWriter(directory, conf);
    int count = 0;
    int n = 0;
    Document doc ;
    while (count++ < 5555555) {
      doc = new Document();
      doc.add(new Field("content", getRandomValue(), type));
      if(count % 2 == 0){
        doc.add(new Field("content", getRandomValue(), type));
      }


//      doc.add(new Field("content", "abc", type));
//      doc.add(new Field("content", "cd", type));
//      doc.add(new StoredField("content", 3));
//      doc.add(new Field("author", "efg", type));

      // 0
      doc = new Document();
      doc.add(new Field("content", "abc", type));
      doc.add(new Field("content", "cd", type));
      doc.add(new StoredField("content", 3));
      doc.add(new Field("author", "efg", type));
      indexWriter.addDocument(doc);
      // 1
      doc = new Document();
      doc.add(new Field("content", "abc", type));
      indexWriter.addDocument(doc);

//
//      doc.add(new SortedDocValuesField("myDocValues", new BytesRef("good")));
//      doc.add(new IntPoint("myIntPoint", 3, 4, 6));
      indexWriter.addDocument(doc);

      if(count % 800 == 0){
        System.out.println("count is "+count+"");
        indexWriter.flush();
      }
    }
    indexWriter.commit();

    DirectoryReader  reader = DirectoryReader.open(indexWriter);
    IndexSearcher searcher = new IndexSearcher(reader);
    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    builder.add(new TermQuery(new Term("content", "a")), BooleanClause.Occur.SHOULD);
    Query query = builder.build();


    TotalHitCountCollector collector = new TotalHitCountCollector();

    searcher.search(query, collector);

    Document document  = reader.document(2);
    System.out.println(document.get("content"));

    // Per-top-reader state:
  }

  public static String getSamePrefixRandomValue(String prefix){
    String str="abcdefghijklmnopqrstuvwxyz";
    Random random=new Random();
    StringBuffer sb=new StringBuffer();
    int length = getLength();
    for(int i=0;i<length;i++){
      int number=random.nextInt(25);
      sb.append(prefix);
      sb.append(str.charAt(number));
    }
    return sb.toString();
  }

  public static String getRandomValue(){
    String str="abcdefghijklmnopqrstuvwxyz";
    Random random=new Random();
    StringBuffer sb=new StringBuffer();
    int length = getLength();
    for(int i=0;i<length;i++){
      int number=random.nextInt(25);
      sb.append(str.charAt(number));
    }
    return sb.toString();
  }

  public static int getLength(){
    Random random = new Random();
    int length = random.nextInt(5);
    if (length < 3){
      length = length + 3;
    }
    return length;
  }

  public static String getMultiSamePrefixValue(String prefix, int wordNum){
    int valueCount = 0;
    StringBuilder stringBuilder = new StringBuilder();
    while (valueCount++ < wordNum){
      stringBuilder.append(getSamePrefixRandomValue(prefix));
      stringBuilder.append(" ");
    }
    stringBuilder.append("end");
    return stringBuilder.toString();
  }

  public static String getMultiValue(){
    int valueCount = 0;
    StringBuilder stringBuilder = new StringBuilder();
    while (valueCount++ < 99){
      stringBuilder.append(getRandomValue());
      stringBuilder.append(" ");
    }
    stringBuilder.append("end");
    return stringBuilder.toString();
  }

  public static void main(String[] args) throws Exception{
    IndexFileWithManyFieldValues test = new IndexFileWithManyFieldValues();
    test.doIndex();
  }
}
