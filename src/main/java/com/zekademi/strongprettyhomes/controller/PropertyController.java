package com.zekademi.strongprettyhomes.controller;

import com.zekademi.strongprettyhomes.domain.Agent;
import com.zekademi.strongprettyhomes.domain.Property;
import com.zekademi.strongprettyhomes.dto.PropertyDTO;
import com.zekademi.strongprettyhomes.repository.PropertyRepository;
import com.zekademi.strongprettyhomes.service.PropertyService;
import lombok.AllArgsConstructor;
import net.kaczmarzyk.spring.data.jpa.domain.*;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/property")
public class PropertyController {

    private final PropertyService propertyService;
    private final PropertyRepository propertyRepository;

    @GetMapping("/visitors/all")
    public ResponseEntity<List<PropertyDTO>> getAllProperties() {
        List<PropertyDTO> properties = propertyService.fetchAllProperties();
        return new ResponseEntity<List<PropertyDTO>>(properties, HttpStatus.OK);
    }

    @GetMapping("/visitors/{id}")
    public ResponseEntity<PropertyDTO> getPropertyById(@PathVariable Long id) {
        PropertyDTO properties = propertyService.findById(id);
        return new ResponseEntity<>(properties, HttpStatus.OK);
    }

    @PostMapping("/admin/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> addProperty(@RequestParam(value = "agentId") Agent agentId,
                                                            @Valid @RequestBody Property property,
                                                            @RequestParam(value = "detailId") Long detailId) {

        propertyService.add(property, agentId,detailId);
        Map<String, Boolean> map = new HashMap<>();
        map.put("Property created successfully!", true);
        return new ResponseEntity<>(map, HttpStatus.CREATED);
    }

    @PutMapping("/admin/auth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> updateProperty(@RequestParam("id") Long id,
                                                               @Valid @RequestBody Property property,
                                                               @RequestParam("agentId") Long agentId,
                                                               @RequestParam("detailId") Long detailId
    ) {
        propertyService.updateProperty(id, property, agentId, detailId);
        Map<String, Boolean> map = new HashMap<>();
        map.put("success", true);
        return new ResponseEntity<>(map, HttpStatus.OK);

    }

    @DeleteMapping("/admin/{id}/auth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> deleteProperty(@PathVariable Long id) {
        propertyService.removeById(id);
        Map<String, Boolean> map = new HashMap<>();
        map.put("success", true);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @GetMapping("/search")
    public Object searchProperties(
            @And({
                    @Spec(path = "type", params = "type", spec = EqualIgnoreCase.class),
                    @Spec(path = "category", params = "category", spec = EqualIgnoreCase.class),
                    @Spec(path = "bedrooms", params = "bedrooms", spec = LikeIgnoreCase.class),
                    @Spec(path = "bathrooms", params = "bathrooms", spec = LikeIgnoreCase.class),
                    @Spec(path = "area", params = "minArea", spec = GreaterThanOrEqual.class),
                    @Spec(path = "area", params = "maxArea", spec = LessThanOrEqual.class),
                    @Spec(path = "price", params = "lowPrice", spec = GreaterThanOrEqual.class),
                    @Spec(path = "price", params = "highPrice", spec = LessThanOrEqual.class)
            }) @Or({
                    @Spec(path = "country", params = "location", spec = LikeIgnoreCase.class),
                    @Spec(path = "city", params = "location", spec = LikeIgnoreCase.class),
                    @Spec(path = "district", params = "location", spec = LikeIgnoreCase.class),
                    @Spec(path = "address", params = "location", spec = LikeIgnoreCase.class),
            }) Specification<Property> customerNameSpec) {
        return propertyRepository.findAll(customerNameSpec);
    }
    
    @GetMapping("/{id}/like")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Long> setLike(@PathVariable Long id,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("id");
        Long setLikes = propertyService.setLike(id, userId);
        return new ResponseEntity<>(setLikes, HttpStatus.OK);
    }

}
