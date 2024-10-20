package galaxy.export.plugin

class GalaxyUserDetails implements Serializable {

    int id
    String username
    String galaxyKey
    String mailAddress

    static mapping = {
        table 'GALAXY.USERS_DETAILS_FOR_EXPORT_GAL'
        id column: 'ID',
            generator: 'sequence',
            params: [sequence: 'GALAXY.HIBERNATE_ID']
        version false

        username column: 'USERNAME'
        galaxyKey column: 'GALAXY_KEY'
        mailAddress column: 'MAIL_ADDRESS'
    }


    static constraints = {
        id(nullable: false)
    }
}
