package org.openstreetmap.atlas.geography.atlas.complete;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Independent {@link Node} that may contain its own altered data. At scale, use at your own risk.
 *
 * @author matthieun
 */
public class CompleteNode extends Node implements CompleteEntity
{
    private static final long serialVersionUID = -8229589987121555419L;

    /*
     * We need to store the original entity bounds at creation-time. This is so multiple consecutive
     * with(Located) calls can update the aggregate bounds without including the bounds from the
     * overwritten change.
     */
    private Rectangle originalBounds;

    /*
     * This is the aggregate feature bounds. It is a super-bound of the original bounds and the
     * changed bounds, if present. Each time with(Located) is called on this entity, it is
     * recomputed from the original bounds and the new Located bounds.
     */
    private Rectangle aggregateBounds;

    private long identifier;
    private Location location;
    private Map<String, String> tags;
    private SortedSet<Long> inEdgeIdentifiers;
    private SortedSet<Long> outEdgeIdentifiers;
    private Set<Long> relationIdentifiers;

    /**
     * Create a full copy of the given node.
     *
     * @param node
     *            the {@link Node} to deep copy
     * @return the new {@link CompleteNode}
     */
    public static CompleteNode from(final Node node)
    {
        return new CompleteNode(node.getIdentifier(), node.getLocation(), node.getTags(),
                node.inEdges().stream().map(Edge::getIdentifier)
                        .collect(Collectors.toCollection(TreeSet::new)),
                node.outEdges().stream().map(Edge::getIdentifier)
                        .collect(Collectors.toCollection(TreeSet::new)),
                node.relations().stream().map(Relation::getIdentifier).collect(Collectors.toSet()));
    }

    /**
     * Create a shallow copy of a given node. All fields (except the identifier and the geometry)
     * are left null until updated by a with() call.
     *
     * @param node
     *            the {@link Node} to copy
     * @return the new {@link CompleteNode}
     */
    public static CompleteNode shallowFrom(final Node node)
    {
        return new CompleteNode(node.getIdentifier())
                .withInitialBounds(node.getLocation().bounds());
    }

    CompleteNode(final long identifier)
    {
        this(identifier, null, null, null, null, null);
    }

    public CompleteNode(final Long identifier, final Location location,
            final Map<String, String> tags, final SortedSet<Long> inEdgeIdentifiers,
            final SortedSet<Long> outEdgeIdentifiers, final Set<Long> relationIdentifiers)
    {
        super(new EmptyAtlas());

        if (identifier == null)
        {
            throw new CoreException("Identifier can never be null.");
        }

        this.originalBounds = location != null ? location.bounds() : null;
        this.aggregateBounds = this.originalBounds;

        this.identifier = identifier;
        this.location = location;
        this.tags = tags;
        this.inEdgeIdentifiers = inEdgeIdentifiers;
        this.outEdgeIdentifiers = outEdgeIdentifiers;
        this.relationIdentifiers = relationIdentifiers;
    }

    @Override
    public Rectangle bounds()
    {
        return this.aggregateBounds;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof CompleteNode)
        {
            final CompleteNode that = (CompleteNode) other;
            return CompleteEntity.basicEqual(this, that)
                    && Objects.equals(this.getLocation(), that.getLocation())
                    && Objects.equals(this.inEdges(), that.inEdges())
                    && Objects.equals(this.outEdges(), that.outEdges());
        }
        return false;
    }

    @Override
    public long getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public Location getLocation()
    {
        return this.location;
    }

    @Override
    public Map<String, String> getTags()
    {
        return this.tags;
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    @Override
    public SortedSet<Edge> inEdges()
    {
        return this.inEdgeIdentifiers == null ? null
                : this.inEdgeIdentifiers.stream().map(CompleteEdge::new)
                        .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public boolean isSuperShallow()
    {
        return this.location == null && this.inEdgeIdentifiers == null
                && this.outEdgeIdentifiers == null && this.tags == null
                && this.relationIdentifiers == null;
    }

    @Override
    public SortedSet<Edge> outEdges()
    {
        return this.outEdgeIdentifiers == null ? null
                : this.outEdgeIdentifiers.stream().map(CompleteEdge::new)
                        .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public Set<Relation> relations()
    {
        return this.relationIdentifiers == null ? null
                : this.relationIdentifiers.stream().map(CompleteRelation::new)
                        .collect(Collectors.toSet());
    }

    @Override
    public String toString()
    {
        return "BloatedNode [identifier=" + this.identifier + ", inEdgeIdentifiers="
                + this.inEdgeIdentifiers + ", outEdgeIdentifiers=" + this.outEdgeIdentifiers
                + ", location=" + this.location + ", tags=" + this.tags + ", relationIdentifiers="
                + this.relationIdentifiers + "]";
    }

    public CompleteNode withAddedTag(final String key, final String value)
    {
        return withTags(CompleteEntity.addNewTag(getTags(), key, value));
    }

    public CompleteNode withAggregateBoundsExtendedUsing(final Rectangle bounds)
    {
        if (this.aggregateBounds == null)
        {
            this.aggregateBounds = bounds;
        }
        this.aggregateBounds = Rectangle.forLocated(this.aggregateBounds, bounds);
        return this;
    }

    public CompleteNode withIdentifier(final long identifier)
    {
        this.identifier = identifier;
        return this;
    }

    public CompleteNode withInEdgeIdentifierExtra(final Long extraInEdgeIdentifier)
    {
        this.inEdgeIdentifiers.add(extraInEdgeIdentifier);
        return this;
    }

    public CompleteNode withInEdgeIdentifierLess(final Long lessInEdgeIdentifier)
    {
        this.inEdgeIdentifiers.remove(lessInEdgeIdentifier);
        return this;
    }

    public CompleteNode withInEdgeIdentifierReplaced(final Long beforeInEdgeIdentifier,
            final Long afterInEdgeIdentifier)
    {
        return this.withInEdgeIdentifierLess(beforeInEdgeIdentifier)
                .withInEdgeIdentifierExtra(afterInEdgeIdentifier);
    }

    public CompleteNode withInEdgeIdentifiers(final SortedSet<Long> inEdgeIdentifiers)
    {
        this.inEdgeIdentifiers = inEdgeIdentifiers;
        return this;
    }

    public CompleteNode withInEdges(final Set<Edge> inEdges)
    {
        this.inEdgeIdentifiers = inEdges.stream().map(Edge::getIdentifier)
                .collect(Collectors.toCollection(TreeSet::new));
        return this;
    }

    public CompleteNode withLocation(final Location location)
    {
        this.location = location;
        if (this.originalBounds == null)
        {
            this.originalBounds = location.bounds();
        }
        this.aggregateBounds = Rectangle.forLocated(this.originalBounds, location.bounds());
        return this;
    }

    public CompleteNode withOutEdgeIdentifierExtra(final Long extraOutEdgeIdentifier)
    {
        this.outEdgeIdentifiers.add(extraOutEdgeIdentifier);
        return this;
    }

    public CompleteNode withOutEdgeIdentifierLess(final Long lessOutEdgeIdentifier)
    {
        this.outEdgeIdentifiers.remove(lessOutEdgeIdentifier);
        return this;
    }

    public CompleteNode withOutEdgeIdentifierReplaced(final Long beforeOutEdgeIdentifier,
            final Long afterOutEdgeIdentifier)
    {
        return this.withOutEdgeIdentifierLess(beforeOutEdgeIdentifier)
                .withOutEdgeIdentifierExtra(afterOutEdgeIdentifier);
    }

    public CompleteNode withOutEdgeIdentifiers(final SortedSet<Long> outEdgeIdentifiers)
    {
        this.outEdgeIdentifiers = outEdgeIdentifiers;
        return this;
    }

    public CompleteNode withOutEdges(final Set<Edge> outEdges)
    {
        this.outEdgeIdentifiers = outEdges.stream().map(Edge::getIdentifier)
                .collect(Collectors.toCollection(TreeSet::new));
        return this;
    }

    public CompleteNode withRelationIdentifiers(final Set<Long> relationIdentifiers)
    {
        this.relationIdentifiers = relationIdentifiers;
        return this;
    }

    public CompleteNode withRelations(final Set<Relation> relations)
    {
        this.relationIdentifiers = relations.stream().map(Relation::getIdentifier)
                .collect(Collectors.toSet());
        return this;
    }

    public CompleteNode withRemovedTag(final String key)
    {
        return withTags(CompleteEntity.removeTag(getTags(), key));
    }

    public CompleteNode withReplacedTag(final String oldKey, final String newKey,
            final String newValue)
    {
        return withRemovedTag(oldKey).withAddedTag(newKey, newValue);
    }

    public CompleteNode withTags(final Map<String, String> tags)
    {
        this.tags = tags;
        return this;
    }

    private CompleteNode withInitialBounds(final Rectangle bounds)
    {
        this.originalBounds = bounds;
        this.aggregateBounds = bounds;
        return this;
    }
}
