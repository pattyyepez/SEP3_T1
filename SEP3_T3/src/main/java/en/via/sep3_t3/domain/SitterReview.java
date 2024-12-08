package en.via.sep3_t3.domain;

import org.springframework.data.annotation.Id;
import java.time.LocalDateTime;

public class SitterReview
{
  @Id private int owner_id;
  @Id private int sitter_id;
  private int rating;
  private String comment;
  private LocalDateTime date;

  public int getOwner_id()
  {
    return owner_id;
  }

  public void setOwner_id(int owner_id)
  {
    this.owner_id = owner_id;
  }

  public int getSitter_id()
  {
    return sitter_id;
  }

  public void setSitter_id(int sitter_id)
  {
    this.sitter_id = sitter_id;
  }

  public int getRating()
  {
    return rating;
  }

  public String getComment()
  {
    return comment;
  }

  public LocalDateTime getDate()
  {
    return date;
  }

  public void setDate(LocalDateTime date)
  {
    this.date = date;
  }

  public void setComment(String comment)
  {
    this.comment = comment;
  }

  public void setRating(int rating)
  {
    this.rating = rating;
  }
  @Override
  public String toString() {
    return "SitterReview{" +
        " HouseSitter id='" + sitter_id + '\'' +
        ", HouseOwner id=" + owner_id +
        ", Comment='" + comment + '\'' +
        ", Rating='" + rating + '\'' +
        ", Date=" + date +
        '}';
  }
}
