package team.projectpulse.instructor;

import team.projectpulse.course.Course;
import team.projectpulse.section.Section;
import team.projectpulse.team.Team;
import team.projectpulse.user.PeerEvaluationUser;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Instructor extends PeerEvaluationUser {

    @OneToMany(mappedBy = "instructor")
    private List<Team> teams = new ArrayList<>();
    @ManyToMany(mappedBy = "instructors")
    private Set<Section> sections = new HashSet<>();
    @ManyToMany(mappedBy = "instructors")
    private Set<Course> courses = new HashSet<>();
    @ManyToOne
    private Section defaultSection; // default section for this instructor, personal preference
    @ManyToOne
    private Course defaultCourse; // default course for this instructor, personal preference


    public Instructor() {
        // Required by JPA to instantiate the entity before populating persistent state.
    }

    public Instructor(String username, String firstName, String lastName, String email, String password, boolean enabled, String roles) {
        super(username, firstName, lastName, email, password, enabled, roles);
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams == null ? null : new ArrayList<>(teams);
    }

    public Set<Section> getSections() {
        return sections;
    }

    public void setSections(Set<Section> sections) {
        this.sections = sections == null ? null : new LinkedHashSet<>(sections);
    }

    public Set<Course> getCourses() {
        return courses;
    }

    public void setCourses(Set<Course> courses) {
        this.courses = courses == null ? null : new LinkedHashSet<>(courses);
    }

    public Section getDefaultSection() {
        return defaultSection;
    }

    public void setDefaultSection(Section defaultSection) {
        this.defaultSection = defaultSection;
    }

    public Course getDefaultCourse() {
        return defaultCourse;
    }

    public void setDefaultCourse(Course defaultCourse) {
        this.defaultCourse = defaultCourse;
    }

}
