package org.openstreetmap.atlas.geography.atlas.bloated;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedAtlas.BloatedEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Independent {@link Edge} that contains its own data. At scale, use at your own risk.
 *
 * @author matthieun
 */
public class BloatedEdge extends Edge implements BloatedEntity
{
    private static final long serialVersionUID = 309534717673911086L;

    private Rectangle bounds;

    private long identifier;
    private PolyLine polyLine;
    private Map<String, String> tags;
    private Long startNodeIdentifier;
    private Long endNodeIdentifier;
    private Set<Long> relationIdentifiers;

    public static BloatedEdge fromEdge(final Edge edge)
    {
        return new BloatedEdge(edge.getIdentifier(), edge.asPolyLine(), edge.getTags(),
                edge.start().getIdentifier(), edge.end().getIdentifier(),
                edge.relations().stream().map(Relation::getIdentifier).collect(Collectors.toSet()));
    }

    public static BloatedEdge shallowFromEdge(final Edge edge)
    {
        return new BloatedEdge(edge.getIdentifier()).withBounds(edge.asPolyLine().bounds());
    }

    BloatedEdge(final long identifier)
    {
        this(identifier, null, null, null, null, null);
    }

    public BloatedEdge(final Long identifier, final PolyLine polyLine,
            final Map<String, String> tags, final Long startNodeIdentifier,
            final Long endNodeIdentifier, final Set<Long> relationIdentifiers)
    {
        super(new BloatedAtlas());

        if (identifier == null)
        {
            throw new CoreException("Identifier is the only parameter that cannot be null.");
        }

        this.bounds = polyLine == null ? null : polyLine.bounds();

        this.identifier = identifier;
        this.polyLine = polyLine;
        this.tags = tags;
        this.startNodeIdentifier = startNodeIdentifier;
        this.endNodeIdentifier = endNodeIdentifier;
        this.relationIdentifiers = relationIdentifiers;
    }

    @Override
    public PolyLine asPolyLine()
    {
        return this.polyLine;
    }

    @Override
    public Rectangle bounds()
    {
        return this.bounds;
    }

    @Override
    public Node end()
    {
        return this.endNodeIdentifier == null ? null : new BloatedNode(this.endNodeIdentifier);
    }

    @Override
    public boolean equals(final Object other)
    {
        return BloatedAtlas.equals(this, other);
    }

    @Override
    public long getIdentifier()
    {
        return this.identifier;
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
    public Set<Relation> relations()
    {
        return this.relationIdentifiers == null ? null
                : this.relationIdentifiers.stream().map(BloatedRelation::new)
                        .collect(Collectors.toSet());
    }

    @Override
    public Node start()
    {
        return this.startNodeIdentifier == null ? null : new BloatedNode(this.startNodeIdentifier);
    }

    public BloatedEdge withEndNodeIdentifier(final Long endNodeIdentifier)
    {
        this.endNodeIdentifier = endNodeIdentifier;
        return this;
    }

    public BloatedEdge withIdentifier(final long identifier)
    {
        this.identifier = identifier;
        return this;
    }

    public BloatedEdge withPolyLine(final PolyLine polyLine)
    {
        this.polyLine = polyLine;
        this.bounds = polyLine.bounds();
        return this;
    }

    public BloatedEdge withRelationIdentifiers(final Set<Long> relationIdentifiers)
    {
        this.relationIdentifiers = relationIdentifiers;
        return this;
    }

    public BloatedEdge withStartNodeIdentifier(final Long startNodeIdentifier)
    {
        this.startNodeIdentifier = startNodeIdentifier;
        return this;
    }

    public BloatedEdge withTags(final Map<String, String> tags)
    {
        this.tags = tags;
        return this;
    }

    private BloatedEdge withBounds(final Rectangle bounds)
    {
        this.bounds = bounds;
        return this;
    }
}